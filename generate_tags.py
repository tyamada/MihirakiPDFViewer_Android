import subprocess
import re

def run_git(args):
    res = subprocess.run(["git"] + args, capture_output=True, text=True)
    return res.stdout.strip()

def get_commits():
    # Get git log in chronological order (oldest to newest)
    res = subprocess.run(["git", "log", "--reverse", "--format=%H|%s"], capture_output=True, text=True)
    return [line.split("|", 1) for line in res.stdout.splitlines() if "|" in line]

def get_file_content(commit_hash, filepath):
    res = subprocess.run(["git", "show", f"{commit_hash}:{filepath}"], capture_output=True, text=True)
    return res.stdout if res.returncode == 0 else ""

def get_commits_between(start_commit, end_commit):
    if start_commit:
        cmd = ["git", "log", "--format=- %s", f"{start_commit}..{end_commit}"]
    else:
        cmd = ["git", "log", "--format=- %s", end_commit]
    res = subprocess.run(cmd, capture_output=True, text=True)
    return res.stdout.strip()

def version_key(v_tuple):
    v_str = v_tuple[0]
    parts = []
    for p in re.split(r'[\.\-]', v_str):
        if p.isdigit():
            parts.append(int(p))
        else:
            parts.append(p)
    return parts

def main():
    commits = get_commits()
    version_release_points = []
    last_vname = None

    print("Analyzing git history for version changes...")
    for commit_hash, subject in commits:
        content = get_file_content(commit_hash, "app/build.gradle.kts")
        match = re.search(r'versionName\s*=\s*"([^"]+)"', content)
        if match:
            vname = match.group(1)
            if vname != last_vname or "release" in subject.lower() or "bump" in subject.lower() or "bundle" in subject.lower():
                version_release_points.append((commit_hash, vname, subject))
                last_vname = vname

    releases = {}
    for h, v, s in version_release_points:
        if v not in releases:
            releases[v] = (h, s)
        else:
            if "aab" in s.lower() or "bump" in s.lower() or "release" in s.lower():
                releases[v] = (h, s)

    sorted_releases = sorted(releases.items(), key=version_key)

    prev_commit = None
    for vname, (commit_hash, subject) in sorted_releases:
        tag_name = f"v{vname}"

        existing_tags = run_git(["tag", "-l", tag_name])
        if existing_tags:
            print(f"Tag {tag_name} already exists. Skipping.")
            prev_commit = commit_hash
            continue

        changelog = get_commits_between(prev_commit, commit_hash)
        tag_message = f"Release {tag_name}\n\nChanges:\n{changelog}"

        print(f"Creating tag {tag_name} at {commit_hash[:7]} ({subject})")
        subprocess.run(["git", "tag", "-a", tag_name, "-m", tag_message, commit_hash])
        prev_commit = commit_hash

    print("All tags created successfully!")
    print("To push tags to remote repository, run: git push origin --tags")

if __name__ == "__main__":
    main()
