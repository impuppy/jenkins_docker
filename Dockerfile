FROM nginx
RUN apt-get update && apt-get install -y procps
WORKDIR /usr/share/nginx/arhipov
COPY index.html /usr/share/nginx/arhipov
CMD cd/usr/share/nginx/arhipov && sed -e s/Docker/"$AUTHOR"/ index.html > index.html ; nginx -g 'daemon off;'
