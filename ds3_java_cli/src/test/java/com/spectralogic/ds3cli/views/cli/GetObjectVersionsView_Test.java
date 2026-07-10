package com.spectralogic.ds3cli.views.cli;

import com.google.common.collect.ImmutableList;
import com.spectralogic.ds3cli.models.GetObjectVersionsResult;
import com.spectralogic.ds3client.models.S3Object;
import org.junit.Test;
import java.util.UUID;
import static org.hamcrest.CoreMatchers.containsString;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class GetObjectVersionsView_Test {

    @Test
    public void testRender() {
        final String versionId = UUID.randomUUID().toString();
        final String bucketId = UUID.randomUUID().toString();
        
        final S3Object version = mock(S3Object.class);
        when(version.getId()).thenReturn(UUID.fromString(versionId));
        when(version.getName()).thenReturn("test-object");
        when(version.getLatest()).thenReturn(true);
        when(version.getBucketId()).thenReturn(UUID.fromString(bucketId));
        
        final GetObjectVersionsResult result = new GetObjectVersionsResult(ImmutableList.of(version));
        final GetObjectVersionsView view = new GetObjectVersionsView();
        final String rendered = view.render(result);
        
        final String[] lines = rendered.split("\n");
        boolean foundHeader = false;
        boolean foundDataLine = false;
        for (final String line : lines) {
            if (line.contains("Bucket Id") && line.contains("Version Id")) {
                foundHeader = true;
                final String[] headerParts = line.split("\\|");
                assertTrue("Bucket Id header should be in first column", headerParts[1].contains("Bucket Id"));
                assertTrue("Version Id header should be in fifth column", headerParts[5].contains("Version Id"));
            }
            if (line.contains("test-object")) {
                foundDataLine = true;
                final String[] dataParts = line.split("\\|");
                
                // Assert that they are in the CORRECT columns.
                // Index 1 is Bucket Id, Index 5 is Version Id (split starts from before the first |)
                
                assertTrue("Bucket ID should be in the first column", dataParts[1].trim().equals(bucketId));
                assertTrue("Version ID should be in the fifth column", dataParts[5].trim().equals(versionId));
            }
        }
        assertTrue("Should have found header", foundHeader);
        assertTrue("Should have found data line", foundDataLine);
    }
}
