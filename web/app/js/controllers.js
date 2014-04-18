"use strict";

/* Controllers */

var bsDataApp = angular.module("bsdataApp.controllers", ["ngResource"]);


bsDataApp.service("repoRestApi", function($resource) {
    return $resource("/repos/:id",
            {},
            {
                list: {method: "GET", params: {}},
                get: {method: "GET", params: {}}
            });
});

bsDataApp.controller("ReposCtrl", function($scope, repoRestApi) {
    $scope.m = {
        feedUrl: "",
        repos: []
    };

    repoRestApi.list({}, function(data) {
        $scope.m.feedUrl = data.feedUrl;
        $scope.m.repos = [];
        angular.forEach(data.repositories, function(repo) {
            $scope.m.repos.push(repo);
        });
    });
});

bsDataApp.controller("RepoCtrl", function($scope, $routeParams, repoRestApi, $fileUploader, $http) {
    $scope.m = {
        repoName : $routeParams.repoName
    };

    repoRestApi.get({id: $routeParams.repoName}, function(data) {
        $scope.m.repo = data;
    });
});

bsDataApp.controller("FileFormCtrl", function($scope, $routeParams, repoRestApi, $fileUploader, $http) {
    
    $scope.formData = {
        currentFile : null,
        isIssue : false,
        isUpload : false,
        commitMessage : "",
        issueBody : "",
        formResponse : null
    };
    
    $scope.setCurrentFile = function(repoFile) {
        $scope.clearData();
        $scope.formData.currentFile = repoFile;
    };
    
    $scope.showUploadForm = function(repoFile) {
        $scope.setCurrentFile(repoFile);
        $scope.createUploader();
        $scope.formData.isUpload = true;
        $scope.formData.isIssue = false;
    };
    
    $scope.showIssueForm = function(repoFile) {
        $scope.setCurrentFile(repoFile);
        $scope.formData.isUpload = false;
        $scope.formData.isIssue = true;
    };
    
    $scope.cancelForm = function() {
        $scope.formData.isUpload = false;
        $scope.formData.isIssue = false;
        $scope.clearData();
    };
    
    $scope.clearData = function() {
        $scope.formData.currentFile = null;
        $scope.formData.issueBody = "";
        $scope.formData.commitMessage = "";
        $scope.formData.formResponse = null;
        $scope.uploader = null;
    };
    
    $scope.createUploader = function() {
        $scope.uploader = $fileUploader.create({
            scope: $scope,
            removeAfterUpload : true
        });
        $scope.uploader.queueLimit = 1;
        $scope.uploader.bind("success", function (event, xhr, item, response) {
            if (response.successMessage) {
                $scope.formData.isUpload = false;
                $scope.clearData();
            }
            $scope.formData.formResponse = response;
        });
        $scope.uploader.bind("error", function (event, xhr, item, response) {
            $scope.formData.formResponse = response;
        });
    };

    $scope.doUpload = function() {
        if ($scope.formData.isUpload && $scope.uploader.queue && $scope.uploader.queue.length === 1) {
            var item = $scope.uploader.queue[0];
            item.url = $scope.formData.currentFile.dataFileUrl;
            item.formData = [
                { commitMessage : $scope.formData.commitMessage }
            ];
            $scope.uploader.uploadItem(0);
        }
    };

    $scope.postBug = function() {
        if ($scope.formData.isIssue) {
            var formData = new FormData();
            formData.append("issueBody", $scope.formData.issueBody);
            $http({
                method : "POST",
                url : $scope.formData.currentFile.issueUrl,
                data : formData,
                transformRequest : angular.identity,
                headers : {
                    "Content-Type" : undefined 
                }
            })
            .success(function(data) {
                if (data.successMessage) {
                    $scope.formData.isIssue = false;
                    $scope.clearData();
                }
                $scope.formData.formResponse = data;
            })
            .error(function(data) {
                $scope.formData.formResponse = data;
            });
        }
    };
});

bsDataApp.directive('validFile', function() {
    return {
        require: 'ngModel',
        link: function(scope, el, attrs, ngModel) {
            ngModel.$render = function () {
                ngModel.$setViewValue(el.val());
            };

            el.bind('change', function () {
                scope.$apply(function () {
                    ngModel.$render();
                });
            });
        }
    }
});