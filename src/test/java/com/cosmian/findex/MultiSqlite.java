/*
 *package com.cosmian.findex;
 *
 *import java.sql.SQLException;
 *import java.util.ArrayList;
 *import java.util.HashMap;
 *import java.util.List;
 *import java.util.Map;
 *import java.util.Set;
 *
 *import com.cosmian.jna.findex.Index;
 *import com.cosmian.jna.findex.serde.Tuple;
 *import com.cosmian.jna.findex.structs.ChainTableValue;
 *import com.cosmian.jna.findex.structs.EntryTableValue;
 *import com.cosmian.jna.findex.structs.EntryTableValues;
 *import com.cosmian.jna.findex.structs.Uid32;
 *import com.cosmian.utils.CloudproofException;
 *
 *public class MultiSqlite extends Index {
 *
 *    private final List<SqliteRemovedUserDb> dbList;
 *
 *    public MultiSqlite(List<SqliteRemovedUserDb> dbList) throws SQLException {
 *        this.dbList = dbList;
 *    }
 *
 *    @Override
 *    protected List<Tuple<Uid32, EntryTableValue>> fetchEntries(List<Uid32> uids) throws CloudproofException {
 *        List<Tuple<Uid32, EntryTableValue>> output = new ArrayList<Tuple<Uid32, EntryTableValue>>();
 *        for (SqliteRemovedUserDb sqlite : this.dbList) {
 *            List<Tuple<Uid32, EntryTableValue>> entries = sqlite.fetchEntries(uids);
 *            output.addAll(entries);
 *        }
 *
 *        return output;
 *    }
 *
 *    @Override
 *    protected Map<Uid32, ChainTableValue> fetchChains(List<Uid32> uids) throws CloudproofException {
 *        HashMap<Uid32, ChainTableValue> output = new HashMap<>();
 *        for (SqliteRemovedUserDb sqlite : this.dbList) {
 *            Map<Uid32, ChainTableValue> chains = sqlite.fetchChains(uids);
 *            output.putAll(chains);
 *        }
 *        return output;
 *    }
 *
 *    protected Set<Uid32> fetchAllEntryTableUids() throws CloudproofException {
 *        throw new CloudproofException("not implemented");
 *    }
 *
 *    @Override
 *    protected Map<Uid32, EntryTableValue> upsertEntries(Map<Uid32, EntryTableValues> uidsAndValues)
 *        throws CloudproofException {
 *        throw new CloudproofException("not implemented");
 *    }
 *
 *    @Override
 *    protected void insertChains(Map<Uid32, ChainTableValue> uidsAndValues) throws CloudproofException {
 *        throw new CloudproofException("not implemented");
 *    }
 *
 *    @Override
 *    protected void deleteEntries(List<Uid32> uids) throws CloudproofException {
 *        throw new CloudproofException("not implemented");
 *    }
 *
 *    @Override
 *    protected void deleteChains(List<Uid32> uids) throws CloudproofException {
 *        throw new CloudproofException("not implemented");
 *    }
 *
 *}
 */
