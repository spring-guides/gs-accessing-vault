#!/bin/sh
cd $(dirname $0)

# Launch vault
mkdir -p target
cd target

/vault server --dev --dev-root-token-id="00000000-0000-0000-0000-000000000000" &
sleep 1

export export VAULT_TOKEN="00000000-0000-0000-0000-000000000000"
export VAULT_ADDR="http://127.0.0.1:8200"

/vault write secret/github github.oauth2.key=foobar
cd ..

cd ../complete

./mvnw clean package
ret=$?
if [ $ret -ne 0 ]; then
  exit $ret
fi
rm -rf target

./gradlew build
ret=$?
if [ $ret -ne 0 ]; then
  exit $ret
fi
rm -rf build

cd ../initial

./mvnw clean compile
ret=$?
if [ $ret -ne 0 ]; then
  exit $ret
fi
rm -rf target

./gradlew compileJava
ret=$?
if [ $ret -ne 0 ]; then
  exit $ret
fi
rm -rf build

pkill vault
exit
