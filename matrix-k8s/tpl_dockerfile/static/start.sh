sed -i "s/NGINX_ENV/$NGINX_ENV/g" /etc/nginx/conf.d/default.conf

nginx -g 'daemon off;'
