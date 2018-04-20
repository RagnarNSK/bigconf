export class UsersList  {

    constructor(container) {
        this.container = container;
    }

    init() {
        $(this.container).append("Initialized");
    }

}
