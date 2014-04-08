'use strict';

/* Controllers */

angular.module('bsdataApp.controllers', ["ngResource"])
  .service("repoRestApi", function($resource) {
    return $resource("/repos/:id", 
      {}, 
      {
          list : {method: 'GET', params:{}},
          get: {method: 'GET', params:{}}
      });
  })
  .controller('ReposCtrl', function($scope, repoRestApi) {
    $scope.m = {
        feedUrl : "",
        repos : []
    };
    
    repoRestApi.list({}, function(data) {
        $scope.m.feedUrl = data.feedUrl;
        $scope.m.repos = [];
        angular.forEach(data.repositories, function(repo) {
            $scope.m.repos.push(repo);
        });
    });
  })
  .controller('RepoCtrl', function($scope, $routeParams, repoRestApi) {
    $scope.m = {
        repoName : $routeParams.repoName
    };
    repoRestApi.get({id: $routeParams.repoName}, function(data) {
        $scope.m.repo = data;
    });
  });