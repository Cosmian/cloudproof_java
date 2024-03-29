package com.cosmian.jna.findex.ffi;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import com.cosmian.jna.findex.serde.Leb128Serializable;
import com.cosmian.jna.findex.structs.Keyword;
import com.cosmian.jna.findex.structs.Location;
import com.cosmian.utils.CloudproofException;
import com.cosmian.utils.Leb128;

public class SearchResults implements Leb128Serializable {

    private Map<Keyword, Set<Location>> results;

    public SearchResults() {
        this.results = new HashMap<>();
    }

    public Map<Keyword, Set<Location>> getResults() {
        return results;
    }

    public boolean isEmpty() {
        return results.isEmpty();
    }

    public int numberOfKeywords() {
        return results.size();
    }

    public int numberOfUniqueLocations() {
        return getLocations().size();
    }

    public Set<Location> get(Keyword keyword) {
        return results.getOrDefault(keyword, new HashSet<>());
    }

    public Set<Location> getLocations() {
        return results.values()
            .stream()
            .flatMap(locations -> locations.stream())
            .collect(Collectors.toSet());
    }

    public Set<String> getStrings() {
        return results.values()
            .stream()
            .flatMap(locations -> locations.stream().map(location -> location.toString()))
            .collect(Collectors.toSet());
    }

    public Set<Long> getNumbers() {
        return results.values()
            .stream()
            .flatMap(locations -> locations.stream().map(location -> location.toNumber()))
            .collect(Collectors.toSet());
    }

    public Set<UUID> getUuids() {
        return results.values()
            .stream()
            .flatMap(locations -> locations.stream().map(location -> location.toUuid()))
            .collect(Collectors.toSet());
    }

    public Set<String> getStrings(Keyword keyword) {
        return results.getOrDefault(keyword, new HashSet<>())
            .stream()
            .map(location -> location.toString())
            .collect(Collectors.toSet());
    }

    public Set<Long> getNumbers(Keyword keyword) {
        return results.getOrDefault(keyword, new HashSet<>())
            .stream()
            .map(location -> location.toNumber())
            .collect(Collectors.toSet());
    }

    public Set<UUID> getUuids(Keyword keyword) {
        return results.getOrDefault(keyword, new HashSet<>())
            .stream()
            .map(location -> location.toUuid())
            .collect(Collectors.toSet());
    }

    public Set<String> getStrings(String keyword) {
        return this.getStrings(new Keyword(keyword));
    }

    public Set<Long> getNumbers(String keyword) {
        return this.getNumbers(new Keyword(keyword));
    }

    public Set<UUID> getUuids(String keyword) {
        return this.getUuids(new Keyword(keyword));
    }

    @Override
    public void readObject(InputStream is) throws CloudproofException {
        try {

            int mapLen = (int) Leb128.readU64(is);
            for (int i = 0; i < mapLen; i++) {
                Keyword keyword = new Keyword(Leb128.readByteArray(is));
                int numResults = (int) Leb128.readU64(is);
                Set<Location> locations = new HashSet<>();
                for (int j = 0; j < numResults; j++) {
                    locations.add(new Location(Leb128.readByteArray(is)));
                }
                results.put(keyword, locations);
            }
        } catch (IOException e) {
            throw new CloudproofException("failed deserializing the search results: " + e.getMessage(), e);
        }

    }

    @Override
    public void writeObject(OutputStream os) throws CloudproofException {
        throw new CloudproofException("Search Results are not serializable");
    }

}
