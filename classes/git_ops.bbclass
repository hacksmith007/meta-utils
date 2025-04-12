
def get_git_commit_remote(branch, repo_name):
    import subprocess
    import bb
    try:
        command = f"git ls-remote https://github.com/hacksmith007/{repo_name}.git refs/heads/{branch} | awk '{{print $1}}'"
        commit_hash = subprocess.check_output(command, shell=True, text=True).strip()
        bb.note(f"Commit hash for branch {branch} in repo {repo_name}: {commit_hash}")
        return commit_hash
    except subprocess.CalledProcessError as e:
        raise RuntimeError(f"Failed to get commit hash for branch {branch} in repo {repo_name}: {e}")

def get_git_commit_local(path):
    import subprocess
    import bb
    try:
        return subprocess.check_output(["git", "-C", path, "rev-parse", "HEAD"]).decode("utf-8").strip()
    except Exception as e:
        bb.warn(f"⚠️ Failed to get git commit from {path}: {e}")
        return ""