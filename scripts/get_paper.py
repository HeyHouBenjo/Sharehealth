import requests
import sys


def get_paper(version, destination):
    print(f"Trying to fetch {version} into {destination}")

    base = "https://papermc.io/api/v2/projects/paper/versions"
    r = requests.get(f"{base}/{version}")
    if not r.ok:
        print(f"Cannot find version {version}, aborting.")
        return

    build = max(r.json()['builds'])
    r = requests.get(f"{base}/{version}/builds/{build}")

    download = r.json()['downloads']['application']['name']
    content = requests.get(f"{base}/{version}/builds/{build}/downloads/{download}").content

    with open(destination, "wb") as jarFile:
        jarFile.write(content)


def main(argv):
    if len(argv) == 1:
        print("No version defined!")
        return
    version = argv[1]
    if len(argv) == 2:
        destination = "paper.jar"
    else:
        destination = argv[2]
    get_paper(version, destination)


if __name__ == "__main__":
    main(sys.argv)
