export const UserService = ['restRoutes', function (restRoutes) {
    var instance = {};
    instance.currentUserData = null;
    instance.initializing = false;
    instance.pending = [];
    instance.getCurrentUser = async function () {
        if (!instance.currentUserData) {
            if (!instance.initializing) {
                instance.initializing = true;
                return new Promise((resolve,reject) => {
                        $.getJSON(restRoutes.usersMe)
                            .done(function (data) {
                                instance.currentUserData = data;
                                resolve(data);
                                instance.pending.forEach(promise => {
                                    promise.resolve(data);
                                })
                            })
                            .fail(error => {
                                reject();
                                instance.pending.forEach(promise => {
                                    promise.reject(data);
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
                resolve(instance.currentUserData)
            })
        }
    }
    return instance;
}]


export const ConferenceProcessService = ['restRoutes', function (restRoutes) {

}];