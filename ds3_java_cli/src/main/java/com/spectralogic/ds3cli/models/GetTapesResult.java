package com.spectralogic.ds3cli.models;

import com.spectralogic.ds3client.models.tape.Tapes;

public class GetTapesResult implements Result {

    private final Tapes tapes;

    public GetTapesResult(final Tapes tapes) {
        this.tapes = tapes;
    }

    public Tapes getTapes() {
        return tapes;
    }
}
