/*
 * Copyright 2010-2013 Institut Pasteur.
 * 
 * This file is part of Icy.
 * 
 * Icy is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * Icy is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with Icy. If not, see <http://www.gnu.org/licenses/>.
 */
package icy.search;

/**
 * @author Stephane
 */
public interface SearchResultConsumer
{
    /**
     * Notify consumer than a single result has just been modified.
     */
    public void resultChanged(SearchResultProducer producer, SearchResult result);

    /**
     * Notify consumer than results list changed.
     */
    public void resultsChanged(SearchResultProducer producer);

    /**
     * Notify consumer than search request completed.
     */
    public void searchCompleted(SearchResultProducer producer);
}
