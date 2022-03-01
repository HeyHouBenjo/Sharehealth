import requests

version = "1.18.1"
destPath = "../server/paper.jar"

base = "https://papermc.io/api/v2/projects/paper/versions"
r = requests.get(f"{base}/{version}")

build = max(r.json()['builds'])
r = requests.get(f"{base}/{version}/builds/{build}")

download = r.json()['downloads']['application']['name']
content = requests.get(f"{base}/{version}/builds/{build}/downloads/{download}").content

with open(destPath, "wb") as file:
    file.write(content)
