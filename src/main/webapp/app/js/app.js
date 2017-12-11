'use strict';
angular.module('bsdataApp', [
  'ngRoute',
  'angularFileUpload',
  'bsdataApp.filters',
  'bsdataApp.services',
  'bsdataApp.directives',
  'bsdataApp.controllers'
]).
config(['$routeProvider', function($routeProvider) {
  $routeProvider.when('/repos', {templateUrl: 'app/partials/repos.html', controller: 'ReposCtrl'});
  $routeProvider.when('/repo/:repoName', {templateUrl: 'app/partials/repo.html', controller: 'RepoCtrl'});
  $routeProvider.otherwise({redirectTo: '/repos'});
}]);
