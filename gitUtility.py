#! /usr/bin/python
#
# Various git commands that Dan uses to synchronize the repo state from the GitHub Apelon repository 
# to the other various repositories after doing a build.

# To use this script, you need to have your remote's named "CN", "GH-I" and "GH-A"
#

import subprocess

def git(*args):
    return subprocess.check_call(['git'] + list(args))

print("Fetching Latest")
git("fetch", "CN")
git("fetch", "GH-I")
git("fetch", "GH-A")

print("Pushing develop Branches from GH-A to CN and GH-I")
git("push", "CN", "GH-A/develop:develop")
git("push", "GH-I", "GH-A/develop:develop")

print("Pushing master Branches from GH-A to CN and GH-I")
git("push", "CN", "GH-A/master:master")
git("push", "GH-I", "GH-A/master:master")

print("Pushing Tags to CN")
git("push", "CN", "--tags")

print("Pushing Tags to GH-I")
git("push", "GH-I", "--tags")
