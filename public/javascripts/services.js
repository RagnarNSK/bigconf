class BaseSingleFetchService {
    constructor(url) {
        let instance = this;
        instance.data = null;
        instance.initializing = false;
        instance.pending = [];
        instance.get = async function () {
            if (!instance.data) {
                if (!instance.initializing) {
                    instance.initializing = true;
                    return new Promise((resolve, reject) => {
                            $.getJSON(url)
                                .done(function (data) {
                                    instance.data = data;
                                    resolve(data);
                                    instance.pending.forEach(promise => {
                                        promise.resolve(data);
                                    })
                                })
                                .fail(error => {
                                    reject();
                                    instance.pending.forEach(promise => {
                                        promise.reject();
                                    })
                                });
                        }
                    );
                } else {
                    return new Promise((resolve, reject) => {
                        instance.pending.push({resolve: resolve, reject: reject});
                    });
                }
            } else {
                return new Promise(resolve => {
                    resolve(instance.data)
                })
            }
        }
    }
}


export const UserService = ['restRoutes', function (restRoutes) {
    var instance = {};
    instance.getCurrentUserService = new BaseSingleFetchService(restRoutes.usersMe);
    instance.getCurrentUser = instance.getCurrentUserService.get;

    instance.getAllUsersService = new BaseSingleFetchService(restRoutes.usersList);
    instance.getAllUsers = instance.getAllUsersService.get;
    return instance;
}]


export const ConferenceProcessService = ['restRoutes', function (restRoutes) {

}];