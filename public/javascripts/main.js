import {UsersList} from './home.js';
import {TestConf} from "./testConf.js";

$.when($.ready).then(function () {
   let usersList = $("#root").append('<div id="UsersList"></div>');
   new UsersList(usersList).init();

   new TestConf().init();
});