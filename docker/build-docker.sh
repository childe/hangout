# build
curPath=`pwd`

cd ..

mvn clean package install -DskipTests

echo 'mvn succ'

unzip -q hangout-dist/target/*.zip  -d $curPath/hangout-tmp/

cd $curPath

docker build -f Dockerfile.hangout --tag=hangout:latest --rm=true .

rm -rf hangout-tmp

docker ps -aq -f status=exited | xargs docker rm

# docker ps -aq -f status=running | xargs docker rm -f