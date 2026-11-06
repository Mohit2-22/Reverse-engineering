-- FitTrack — Supabase schema (run in Supabase dashboard → SQL Editor)
-- Enable PostGIS first for GPS geometry support.
create extension if not exists postgis;

-- ---------- Profiles (extends Supabase auth.users) ----------
create table if not exists profiles (
  id uuid primary key references auth.users(id) on delete cascade,
  username text unique,
  full_name text,
  avatar_url text,
  weight_kg numeric,
  created_at timestamptz default now()
);

-- Auto-create a profile row when a user signs up
create or replace function handle_new_user()
returns trigger language plpgsql security definer as $$
begin
  insert into profiles (id, full_name)
  values (new.id, new.raw_user_meta_data->>'full_name');
  return new;
end; $$;

drop trigger if exists on_auth_user_created on auth.users;
create trigger on_auth_user_created
  after insert on auth.users
  for each row execute function handle_new_user();

-- ---------- Activities (one row per recorded workout) ----------
create table if not exists activities (
  id uuid primary key default gen_random_uuid(),
  user_id uuid references profiles(id) on delete cascade not null,
  type text not null,                     -- 'run' | 'ride' | 'walk' | 'hike'
  title text,
  started_at timestamptz not null,
  elapsed_time_s int not null,
  moving_time_s int not null,
  distance_m numeric not null,
  elevation_gain_m numeric,
  avg_speed_mps numeric,
  avg_hr int,
  calories int,
  route geography(LineString, 4326),      -- the GPS polyline
  start_point geography(Point, 4326),
  map_thumbnail_url text,
  created_at timestamptz default now()
);
create index if not exists activities_user_idx on activities(user_id, started_at desc);

-- ---------- Detailed GPS/sensor samples (optional, for charts/flyover) ----------
create table if not exists activity_streams (
  id bigserial primary key,
  activity_id uuid references activities(id) on delete cascade,
  t_offset_s int not null,
  lat double precision,
  lng double precision,
  altitude_m numeric,
  speed_mps numeric,
  heart_rate int
);
create index if not exists streams_activity_idx on activity_streams(activity_id, t_offset_s);

-- ---------- Social (Tier 3) ----------
create table if not exists follows (
  follower_id uuid references profiles(id) on delete cascade,
  following_id uuid references profiles(id) on delete cascade,
  primary key (follower_id, following_id)
);
create table if not exists kudos (
  activity_id uuid references activities(id) on delete cascade,
  user_id uuid references profiles(id) on delete cascade,
  primary key (activity_id, user_id)
);
create table if not exists comments (
  id uuid primary key default gen_random_uuid(),
  activity_id uuid references activities(id) on delete cascade,
  user_id uuid references profiles(id) on delete cascade,
  text text not null,
  created_at timestamptz default now()
);

-- ---------- Row Level Security ----------
alter table profiles          enable row level security;
alter table activities        enable row level security;
alter table activity_streams  enable row level security;
alter table kudos             enable row level security;
alter table comments          enable row level security;
alter table follows           enable row level security;

-- Drop existing policies first so this script is safe to re-run (idempotent).
drop policy if exists "profiles_read"   on profiles;
drop policy if exists "profiles_update" on profiles;
drop policy if exists "activities_own"  on activities;
drop policy if exists "activities_followed_read" on activities;
drop policy if exists "streams_own"     on activity_streams;
drop policy if exists "kudos_read"      on kudos;
drop policy if exists "kudos_write"     on kudos;
drop policy if exists "comments_read"   on comments;
drop policy if exists "comments_write"  on comments;
drop policy if exists "follows_read"    on follows;
drop policy if exists "follows_write"   on follows;

-- Profiles: everyone can read; you can only edit your own
create policy "profiles_read"   on profiles for select using (true);
create policy "profiles_update" on profiles for update using (auth.uid() = id);

-- Activities: you can do anything to your own; read activities of people you follow
create policy "activities_own" on activities
  for all using (auth.uid() = user_id) with check (auth.uid() = user_id);
create policy "activities_followed_read" on activities
  for select using (
    user_id in (select following_id from follows where follower_id = auth.uid())
  );

-- Streams follow their parent activity ownership
create policy "streams_own" on activity_streams
  for all using (
    activity_id in (select id from activities where user_id = auth.uid())
  );

-- Kudos/comments: anyone logged in can read; write as yourself
create policy "kudos_read"   on kudos for select using (true);
create policy "kudos_write"  on kudos for all using (auth.uid() = user_id) with check (auth.uid() = user_id);
create policy "comments_read"  on comments for select using (true);
create policy "comments_write" on comments for all using (auth.uid() = user_id) with check (auth.uid() = user_id);
create policy "follows_read"   on follows for select using (true);
create policy "follows_write"  on follows for all using (auth.uid() = follower_id) with check (auth.uid() = follower_id);

-- ---------- Storage bucket for activity photos (create in dashboard, or:) ----------
insert into storage.buckets (id, name, public) values ('photos', 'photos', true)
  on conflict (id) do nothing;
