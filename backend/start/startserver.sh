(. ../server/venv/bin/activate; cd ../server/src; gunicorn --workers 3 --bind unix:server.sock -m 007 wsgi)