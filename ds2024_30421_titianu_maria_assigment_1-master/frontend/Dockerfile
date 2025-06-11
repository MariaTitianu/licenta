FROM node:18-alpine
WORKDIR /ds2024/frontend
COPY package.json /ds2024/frontend/
RUN npm install
COPY public/ /ds2024/frontend/public
COPY src/ /ds2024/frontend/src
RUN npm run build

EXPOSE 3000
CMD [ "npm", "start" ]
