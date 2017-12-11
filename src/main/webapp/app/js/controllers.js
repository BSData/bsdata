"use strict";

/* Controllers */

var bsDataApp = angular.module("bsdataApp.controllers", ["ngResource", "ngCookies"]);


bsDataApp.service("repoRestApi", function($resource) {
    return $resource("/repos/:id",
            {},
            {
                list: {method: "GET", params: {}},
                get: {method: "GET", params: {}}
            });
});

bsDataApp.controller("ReposCtrl", function($scope, repoRestApi) {
    $scope.showLoading = true;
    $scope.error = false;
    
    $scope.m = {
        name: "LOADING...",
        description: "LOADING...",
        battleScribeVersion: "",
        
        communityUrl: "",
        feedUrl: "",
        twitterUrl: "",
        facebookUrl: "",
        
        repos: []
    };

    repoRestApi.list({}, 
        function(data) {
            $scope.showLoading = false;
            $scope.error = false;
            
            $scope.m.name = data.name;
            $scope.m.description = data.description;
            $scope.m.battleScribeVersion = data.battleScribeVersion;

            $scope.m.communityUrl = data.communityUrl;
            $scope.m.feedUrl = data.feedUrl;
            $scope.m.twitterUrl = data.twitterUrl;
            $scope.m.facebookUrl = data.facebookUrl;
            
            $scope.m.repos = [];
            angular.forEach(data.repositories, function(repo) {
                $scope.m.repos.push(repo);
            });
        },
        function() {
            $scope.showLoading = false;
            $scope.error = true;
        });
});

bsDataApp.controller("RepoCtrl", function($scope, $routeParams, repoRestApi) {
    $scope.m = {
        repoName: $routeParams.repoName
    };

    repoRestApi.get({id: $routeParams.repoName}, function(data) {
        $scope.m.repo = data;
    });

    $scope.pageData = {
        guidelinesAccepted: false
    };
});

bsDataApp.controller("FileFormCtrl", function($scope, $routeParams, repoRestApi, $fileUploader, $http, $window, $cookies) {

    $scope.formData = {
        currentFile: null,
        isIssue: false,
        isUpload: false,
        commitMessage: "",
        battleScribeVersion: "",
        platform: "",
        usingDropbox: null,
        issueBody: "",
        isLoading: false,
        formResponse: null,
        guidelinesAccepted: false,
        showGuidelines : true
    };

    $scope.platforms = [
        "iPhone / iPod / iPad",
        "Android",
        "Windows",
        "Mac",
        "Linux"
    ];

    $scope.isHtml5 = function() {
        return !!($window.File && $window.FormData);
    };

    $scope.showGuidelines = function() {
        $scope.formData.showGuidelines = true;
    };

    $scope.hideGuidelines = function() {
        $scope.formData.showGuidelines = false;
        $scope.$parent.pageData.guidelinesAccepted = $scope.formData.guidelinesAccepted;
    };

    $scope.setCurrentFile = function(repoFile) {
        $scope.formData.guidelinesAccepted = $scope.$parent.pageData.guidelinesAccepted;
        $scope.formData.showGuidelines = !$scope.formData.guidelinesAccepted;
        
        $scope.clearData();
        $scope.formData.currentFile = repoFile;
    };

    $scope.showUploadForm = function(repoFile) {
        $scope.setCurrentFile(repoFile);
        $scope.createUploader();
        $scope.formData.isUpload = true;
        $scope.formData.isIssue = false;
        if (!$scope.formData.guidelinesAccepted) {
            $scope.showGuidelines();
        }
    };

    $scope.showIssueForm = function(repoFile) {
        $scope.setCurrentFile(repoFile);
        $scope.formData.isUpload = false;
        $scope.formData.isIssue = true;
        if (!$scope.formData.guidelinesAccepted) {
            $scope.showGuidelines();
        }
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
        $scope.clearFileUpload();
        $scope.uploader = null;
    };
    
    $scope.clearFileUpload = function() {
        $window.document.getElementById("selectFile-" + this.$index).value = "";
        if ($scope.uploader) {
            $scope.uploader.cancelAll();
            $scope.uploader.clearQueue();
        }
    };

    $scope.createUploader = function() {
        $scope.uploader = $fileUploader.create({
            scope: $scope,
            removeAfterUpload: true
        });
        $scope.uploader.queueLimit = 1;
        $scope.uploader.bind("success", function(event, xhr, item, response) {
            if (response.successMessage) {
                $scope.formData.isUpload = false;
                $scope.clearData();
            }
            else if (response.errorMessage) {
                $scope.clearFileUpload();
            }
            $scope.formData.formResponse = response;
            $scope.formData.isLoading = false;
        });
        $scope.uploader.bind("error", function(event, xhr, item, response) {
            $scope.clearFileUpload();
            $scope.formData.formResponse = response;
            $scope.formData.isLoading = false;
        });
    };

    $scope.isFileSelected = function() {
        if (!$scope.uploader || !$scope.uploader.queue) {
            return false;
        }
        return $scope.uploader.queue.length === 1;
    };

    $scope.doUpload = function() {
        if ($scope.formData.isUpload && $scope.uploader.queue && $scope.uploader.queue.length === 1) {
            $scope.formData.isLoading = true;
            var item = $scope.uploader.queue[0];
            item.url = $scope.formData.currentFile.fileUrl;
            item.formData = [
                {commitMessage: $scope.formData.commitMessage}
            ];
            $scope.uploader.uploadItem(0);
        }
    };

    $scope.postBug = function() {
        if (!$scope.formData.isIssue) {
            return;
        }
        
        $scope.formData.isLoading = true;
        
        var formData = new FormData();
        formData.append("battleScribeVersion", $scope.formData.battleScribeVersion);
        formData.append("platform", $scope.formData.platform);
        formData.append("usingDropbox", $scope.formData.usingDropbox);
        formData.append("issueBody", $scope.formData.issueBody);
        
        $http({
            method: "POST",
            url: $scope.formData.currentFile.reportBugUrl,
            data: formData,
            transformRequest: angular.identity,
            headers: {
                "Content-Type": undefined
            }
        })
        .success(function(data) {
            if (data.successMessage) {
                $scope.formData.isIssue = false;
                $scope.clearData();
            }
            $scope.formData.formResponse = data;
            $scope.formData.isLoading = false;
        })
        .error(function(data) {
            $scope.formData.formResponse = data;
            $scope.formData.isLoading = false;
        });
    };
});
