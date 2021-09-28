## Runtime
### docker-compose
```bash
docker-compose up
```

### local dev
```bash
cd finance-server
lein ring server
cd ../finance-ui
shadow-cljs watch app
```