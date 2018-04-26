export const UsersListComponent = {
    bindings: {
        users: '<',
        onClick: '&'
    },
    template: `<ul><li ng-repeat="user in $ctrl.users" ng-click="$ctrl.userClick(user.id)">{{user.name}}</li></ul>`,
    controller:  function () {
        var ctrl = this;
        ctrl.userClick = function (userId) {
            ctrl.onClick({userId:userId});
        }
    }
}

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
