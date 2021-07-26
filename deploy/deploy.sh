cd ..

read -p "Run tests? [Y/n]
" answer

if [ $answer == "N" ] || [ $answer == "n" ]; then
    rm -r ./src/test
fi

gradle clean build
cd ./build/libs
cp $(ls | grep -v "plain") ../../deploy/
cd ../../deploy
docker-compose up -d