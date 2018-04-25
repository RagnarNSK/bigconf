export const UsersListComponent = {
    template: `<ul><li ng-repeat="user in users" ng-click="userClick(user.id)">{{user.name}}</li></ul>`,
    controller: ['$scope', 'restRoutes', function ($scope, restRoutes) {
        $scope.users = [];
        $.getJSON(restRoutes.usersList).done(function (data) {
            $scope.users = data;
            $scope.$applyAsync();
        });
        $scope.userClick = function (userId) {
            console.log("User " + userId + " clicked");
        }
    }]
};

export const MyUserComponent = {
    template: `
<div class="user-block">
    <h2>Welcome {{user.name}}</h2>
</div>
  `,
    controller: ['$scope', 'restRoutes', function ($scope, restRoutes) {
        $scope.user = {};
        $.getJSON(restRoutes.usersMe).done(function (data) {
            $scope.user = data;
            $scope.$applyAsync();
        });
    }]
};
