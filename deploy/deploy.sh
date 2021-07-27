cd ..

read -p "Run tests? [Y/n]
" answer

FILE=$(ls | grep *.war)
if [ -f "$FILE" ]; then
    rm $FILE
fi

gradle clean
if [ "$answer" = "N" ] || [ "$answer" = "n" ]; then
  gradle assemble
else
  gradle build
fi

cd ./build/libs
cp $(ls | grep -v "plain") ../../deploy/app/
cd ../../deploy
docker-compose up -d