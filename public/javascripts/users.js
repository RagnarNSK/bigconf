export const UsersListComponent = {
    bindings: {
        users: '<',
        onClick: '&'
    },
    template: `<ul><li ng-repeat="user in $ctrl.users" ng-click="userClick(user.id)">{{user.name}}</li></ul>`,
    controller: [ '$scope', function ($scope) {
        const ctrl = this;
        $scope.userClick = function (userId) {
            ctrl.onClick({userId: userId});
        }
    }]
}

export const MyUserComponent = {
    template: `
<div class="user-block">
    <h2>Welcome {{user.name}}</h2>
</div>
  `,
    controller: ['$scope', 'userService', async function ($scope, userService) {
        $scope.user = await userService.getCurrentUser();
    }]
};
