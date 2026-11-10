#!/bin/sh

APP_HOME=$(CDPATH= cd -- "$(dirname -- "$0")" && pwd)
GRADLE_USER_HOME=${GRADLE_USER_HOME:-"$HOME/.gradle"}
DIST_URL=$(sed -n 's/^distributionUrl=//p' "$APP_HOME/gradle/wrapper/gradle-wrapper.properties" | sed 's/\\:/:/g')
DIST_NAME=${DIST_URL##*/}
VERSION=${DIST_NAME%-bin.zip}
INSTALL="$GRADLE_USER_HOME/wrapper/dists/$VERSION/tvkey"
GRADLE="$INSTALL/$VERSION/bin/gradle"
if [ ! -x "$GRADLE" ]; then
  mkdir -p "$INSTALL" || exit 1
  ZIP="$INSTALL/$DIST_NAME"
  if [ ! -f "$ZIP" ]; then
    echo "Downloading Gradle $VERSION..."
    curl -fL "$DIST_URL" -o "$ZIP" || exit 1
  fi
  unzip -q -o "$ZIP" -d "$INSTALL" || exit 1
fi
exec "$GRADLE" "$@"
