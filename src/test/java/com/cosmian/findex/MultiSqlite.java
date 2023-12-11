package com.cosmian.findex;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.cosmian.jna.findex.serde.Tuple;
import com.cosmian.jna.findex.structs.EntryTableValue;
import com.cosmian.jna.findex.structs.EntryTableValues;
import com.cosmian.jna.findex.structs.Uid32;
import com.cosmian.utils.CloudproofException;

public class MultiSqlite extends SqliteEntryTable {

    int selector;

    final List<SqliteEntryTable> entryTableList;

    public MultiSqlite(List<SqliteEntryTable> dbList) throws SQLException {
        if (dbList.isEmpty()) {
            throw new SQLException("No Entry Table given");
        } else {

            this.entryTableList = dbList;
            this.selector = 0;
        }
    }

    public void selectTable(int tableNumber) throws CloudproofException {
        if (this.entryTableList.size() <= tableNumber) {
            throw new CloudproofException("Entry Table number out of range");
        } else {
            this.selector = tableNumber;
        }
    }

    @Override
    public List<Tuple<Uid32, EntryTableValue>> fetch(List<Uid32> uids) throws CloudproofException {
        List<Tuple<Uid32, EntryTableValue>> output = new ArrayList<Tuple<Uid32, EntryTableValue>>();
        for (SqliteEntryTable entryTable : this.entryTableList) {
            List<Tuple<Uid32, EntryTableValue>> entries = entryTable.fetch(uids);
            output.addAll(entries);
        }
        return output;
    }

    @Override
    public Set<Uid32> fetchAllUids() throws CloudproofException {
        return entryTableList.get(selector).fetchAllUids();
    }

    @Override
    public Map<Uid32, EntryTableValue> upsert(Map<Uid32, EntryTableValues> modifications) throws CloudproofException {
        return entryTableList.get(selector).upsert(modifications);
    }

    @Override
    public void delete(List<Uid32> uids) throws CloudproofException {
        entryTableList.get(selector).delete(uids);
    }
}
