'use strict';

/* Controllers */

angular.module('bsdataApp.controllers', ["ngResource"])
  .service("repoRestApi", function($resource) {
    return $resource("/bsdata/repos/:id", 
      {}, 
      {
          list : {method: 'GET', params:{}, isArray: true}
      });
  })
  .controller('ReposCtrl', function($scope, repoRestApi) {
    $scope.m = {
        repos : []
    };
    
    repoRestApi.list({}, function(data) {
        $scope.m.repos = [];
        angular.forEach(data.repos, function(repo) {
            $scope.m.repos.push(repo);
        });
    });
  })
  .controller('RepoCtrl', [function() {

  }]);