package edu.nmhu.bssd5250.sb_maps_demo

class Reminder {
    /**
     * setters and getters
     */
    var id = 0
    var content: String? = null
    var important = 0

    /**
     * Constructors
     */
    constructor() {
        // empty default constructor
    }

    constructor(id: Int, content: String?, important: Int) {
        this.id = id
        this.important = important
        this.content = content
    }
}