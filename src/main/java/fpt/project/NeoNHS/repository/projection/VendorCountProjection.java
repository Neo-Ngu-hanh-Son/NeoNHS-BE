package fpt.project.NeoNHS.repository.projection;

import java.util.UUID;

public interface VendorCountProjection {
    UUID getVendorId();

    Long getCount();
}

