cd ..

read -p "Run tests? [Y/n]
" answer

gradle clean
if [ "$answer" = "N" ] || [ "$answer" = "n" ]; then
  gradle assemble
else
  gradle build
fi

cd ./build/libs
cp $(ls | grep -v "plain") ../../deploy/
cd ../../deploy
docker-compose up -d