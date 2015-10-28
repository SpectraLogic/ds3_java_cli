package com.spectralogic.ds3cli.command;

import com.spectralogic.ds3cli.Arguments;
import com.spectralogic.ds3cli.CommandException;
import com.spectralogic.ds3cli.models.DeleteResult;
import com.spectralogic.ds3cli.util.Ds3Provider;
import com.spectralogic.ds3cli.util.FileUtils;
import com.spectralogic.ds3client.commands.DeleteTapeRequest;
import org.apache.commons.cli.MissingArgumentException;

import java.io.IOException;
import java.util.UUID;

public class DeleteTape extends CliCommand<DeleteResult> {

    private UUID id;

    public DeleteTape(final Ds3Provider ds3Provider, final FileUtils fileUtils) {
        super(ds3Provider, fileUtils);
    }

    @Override
    public CliCommand init(final Arguments args) throws Exception {

        final String idString = args.getId();
        if (idString == null) {
            throw new MissingArgumentException("'-i' is required to delete a tape");
        }

        this.id = UUID.fromString(idString);
        return this;
    }

    @Override
    public DeleteResult call() throws Exception {

        try {
            this.getClient().deleteTape(new DeleteTapeRequest(id));
        }
        catch (final IOException e) {
            throw new CommandException("Error: Request failed with the following error: " + e.getMessage(), e);
        }

        return new DeleteResult("Success: Deleted tape " + id.toString());
    }
}
