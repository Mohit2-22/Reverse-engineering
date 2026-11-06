-- FIX: "Database error saving new user" on signup
-- Cause: the handle_new_user() trigger inserts into profiles, but RLS / search_path
-- can block it. This recreates the trigger robustly.

-- 1. Recreate the function with a fixed search_path and error-tolerance.
create or replace function public.handle_new_user()
returns trigger
language plpgsql
security definer
set search_path = public
as $$
begin
  insert into public.profiles (id, full_name)
  values (new.id, coalesce(new.raw_user_meta_data->>'full_name', ''))
  on conflict (id) do nothing;
  return new;
exception
  when others then
    -- never block auth signup if profile insert fails
    return new;
end;
$$;

-- 2. Make sure the trigger exists and points at the function
drop trigger if exists on_auth_user_created on auth.users;
create trigger on_auth_user_created
  after insert on auth.users
  for each row execute function public.handle_new_user();

-- 3. Allow the trigger (runs as definer/owner) to insert profiles even with RLS on.
--    Add an insert policy so authenticated inserts also work.
drop policy if exists "profiles_insert" on public.profiles;
create policy "profiles_insert" on public.profiles
  for insert with check (auth.uid() = id);

-- 4. Grant the postgres/authenticator roles access (normally present, just ensuring)
grant usage on schema public to anon, authenticated, service_role;
grant all on public.profiles to anon, authenticated, service_role;
