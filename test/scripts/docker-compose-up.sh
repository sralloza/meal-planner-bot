#!/usr/bin/env bash
: ${GITHUB_TOKEN:?Must set \$GITHUB_TOKEN}


export API_VERSION=$(curl -s https://api.github.com/repos/sralloza/chore-management-api/releases/latest -H "Authorization: Bearer $GITHUB_TOKEN" | jq -r ".tag_name" | sed -e "s/v//g")
SCRIPT_DIR=$( cd -- "$( dirname -- "${BASH_SOURCE[0]}" )" &> /dev/null && pwd )

echo "Using API version $API_VERSION"

cd "$SCRIPT_DIR"
docker-compose up -d "$@"
