import {UsersList, MyUserComponent} from './home.js';
import {TestConf} from "./testConf.js";

$.when($.ready).then(function () {
    let usersList = $(`<div id="UsersList"></div>`);
    let myUser = $(`<div id="MyUser"></div>`);

    let root = $("#root");
    root.append(myUser);
    root.append(usersList);

    new MyUserComponent(myUser).init();
    new UsersList(usersList).init();

    new TestConf().init();
});