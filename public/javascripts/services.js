class BaseJSONService {
    constructor(url) {
        this.get = async function () {
            return new Promise((resolve, reject) => {
                $.getJSON(url)
                    .done((data) => {
                        resolve(data);
                    })
                    .fail(() => {
                        reject();
                    });
            })
        }
        this.post = async function () {
            return new Promise((resolve, reject) => {
                $.post(url)
                    .done((data) => {
                        resolve(data);
                    })
                    .fail(() => {
                        reject();
                    });
            })
        }
    }
}

class BaseSingleFetchService {
    constructor(url) {
        const instance = this;
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
    const instance = {};
    instance.getCurrentUser = new BaseSingleFetchService(restRoutes.usersMe).get;
    instance.getAllUsers = new BaseSingleFetchService(restRoutes.usersList).get;
    return instance;
}];


export const ConferenceService = ['restRoutes', function (restRoutes) {
    const instance = {};
    instance.list = new BaseJSONService(restRoutes.conferencesList).get;
    instance.createConference = new BaseJSONService(restRoutes.startConference).post;
    return instance;
}];