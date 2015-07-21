#!/bin/bash

if [[ ${TRAVIS_BRANCH} == 'master' || ${TRAVIS_BRANCH} == 'develop' ]]
then
  echo "Deploying Jar to Maven Central"
  mvn deploy -DskipTests --settings settings.xml -Prelease
else
  echo "Not deploying jar"
fi

if [[ ${TRAVIS_BRANCH} == 'master' ]]
then
  echo "Building Maven Site and deploying to GitHub pages"
  git config credential.helper "store --file=.git/credentials"
  echo "https://${GITHUB_TOKEN}:@github.com" > .git/credentials
  mvn site
  # mvn site used to do this, but now API rate limiting makes it a non starter
  cd target/site
  git config --global user.email "Travis CI"
  git config --global user.name "matthew-gordon-hp@users.noreply.github.com"
  echo "Creating repo"
  git init
  echo "Adding remote"
  git remote add origin "https://${GITHUB_TOKEN}@github.com/${TRAVIS_REPO_SLUG}"
  echo "Adding all the files"
  git add .
  echo "Committing"
  git commit -m "Update GitHub Pages"
  echo "Pushing"
  git push --force origin master:gh-pages > /dev/null 2>&1
fi
