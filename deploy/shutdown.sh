read -p "Delete all images? [y/N]
" answer

docker-compose down

if [ "$answer" = "y" ] || [ "$answer" = "Y" ]; then
  docker rmi anbarinoo deploy_nginx
  #docker rm app db nginx
fi
