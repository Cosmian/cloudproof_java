package com.cosmian.findex;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.cosmian.jna.findex.Database;
import com.cosmian.jna.findex.serde.Tuple;
import com.cosmian.jna.findex.structs.ChainTableValue;
import com.cosmian.jna.findex.structs.EntryTableValue;
import com.cosmian.jna.findex.structs.EntryTableValues;
import com.cosmian.jna.findex.structs.Location;
import com.cosmian.jna.findex.structs.Uid32;
import com.cosmian.utils.CloudproofException;

public class MultiSqlite extends Database {

    private final List<Sqlite> dbList;

    public MultiSqlite(List<Sqlite> dbList) throws SQLException {
        this.dbList = dbList;
    }

    @Override
    protected List<Tuple<Uid32, EntryTableValue>> fetchEntries(List<Uid32> uids) throws CloudproofException {
        List<Tuple<Uid32, EntryTableValue>> output = new ArrayList<Tuple<Uid32, EntryTableValue>>();
        for (Sqlite sqlite : this.dbList) {
            List<Tuple<Uid32, EntryTableValue>> entries = sqlite.fetchEntries(uids);
            output.addAll(entries);
        }

        return output;
    }

    @Override
    protected Map<Uid32, ChainTableValue> fetchChains(List<Uid32> uids) throws CloudproofException {
        HashMap<Uid32, ChainTableValue> output = new HashMap<>();
        for (Sqlite sqlite : this.dbList) {
            Map<Uid32, ChainTableValue> chains = sqlite.fetchChains(uids);
            output.putAll(chains);
        }
        return output;
    }

    @Override
    protected List<Location> filterObsoleteLocations(List<Location> locations) throws CloudproofException {
        throw new CloudproofException("not implemented");
    }

    protected Set<Uid32> fetchAllEntryTableUids() throws CloudproofException {
        throw new CloudproofException("not implemented");
    }

    @Override
    protected Map<Uid32, EntryTableValue> upsertEntries(Map<Uid32, EntryTableValues> uidsAndValues)
        throws CloudproofException {
        throw new CloudproofException("not implemented");
    }

    @Override
    protected void insertChains(Map<Uid32, ChainTableValue> uidsAndValues) throws CloudproofException {
        throw new CloudproofException("not implemented");
    }

    @Override
    protected void updateTables(List<Uid32> removedChains,
                                Map<Uid32, EntryTableValue> newEntries,
                                Map<Uid32, ChainTableValue> newChains)
        throws CloudproofException {
        throw new CloudproofException("not implemented");
    }

    @Override
    protected void deleteEntries(List<Uid32> uids) throws CloudproofException {
        throw new CloudproofException("not implemented");
    }

    @Override
    protected void deleteChains(List<Uid32> uids) throws CloudproofException {
        throw new CloudproofException("not implemented");
    }

}
