/*
 * Copyright 2010, 2011 Institut Pasteur.
 * 
 * This file is part of ICY.
 * 
 * ICY is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * ICY is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with ICY. If not, see <http://www.gnu.org/licenses/>.
 */
package icy.common;

import icy.util.StringUtil;

/**
 * @author stephane
 */
public class Version implements Comparable<Version>
{
    private int major;
    private int minor;
    private int revision;
    private int build;
    private boolean beta;

    public Version()
    {
        this(0, 0, 0, 0, false);
    }

    public Version(int major)
    {
        this(major, 0, 0, 0, false);
    }

    public Version(int major, int minor)
    {
        this(major, minor, 0, 0, false);
    }

    public Version(int major, int minor, int revision)
    {
        this(major, minor, revision, 0, false);
    }

    public Version(int major, int minor, int revision, int build)
    {
        this(major, minor, revision, build, false);
    }

    public Version(int major, int minor, int revision, int build, boolean beta)
    {
        super();

        this.major = major;
        this.minor = minor;
        this.revision = revision;
        this.build = build;
        this.beta = beta;
    }

    public Version(String version)
    {
        this(0, 0, 0, 0, version.toUpperCase().indexOf('B') != -1);

        final String[] values = version.replaceAll("[a-zA-Z ]", "").split("\\.");

        if (values != null)
        {
            if ((values.length > 0) && (!StringUtil.isEmpty(values[0], true)))
                major = Integer.parseInt(values[0]);
            if ((values.length > 1) && (!StringUtil.isEmpty(values[1], true)))
                minor = Integer.parseInt(values[1]);
            if ((values.length > 2) && (!StringUtil.isEmpty(values[2], true)))
                revision = Integer.parseInt(values[2]);
            if ((values.length > 3) && (!StringUtil.isEmpty(values[3], true)))
                build = Integer.parseInt(values[3]);
        }
    }

    /**
     * @return the major
     */
    public int getMajor()
    {
        return major;
    }

    /**
     * @param major
     *        the major to set
     */
    public void setMajor(int major)
    {
        this.major = major;
    }

    /**
     * @return the minor
     */
    public int getMinor()
    {
        return minor;
    }

    /**
     * @param minor
     *        the minor to set
     */
    public void setMinor(int minor)
    {
        this.minor = minor;
    }

    /**
     * @return the revision
     */
    public int getRevision()
    {
        return revision;
    }

    /**
     * @param revision
     *        the revision to set
     */
    public void setRevision(int revision)
    {
        this.revision = revision;
    }

    /**
     * @return the build
     */
    public int getBuild()
    {
        return build;
    }

    /**
     * @param build
     *        the build to set
     */
    public void setBuild(int build)
    {
        this.build = build;
    }

    /**
     * @return the beta
     */
    public boolean isBeta()
    {
        return beta;
    }

    /**
     * @param beta
     *        the beta to set
     */
    public void setBeta(boolean beta)
    {
        this.beta = beta;
    }

    /**
     * special isEmpty case (0.0.0.0)
     */
    public boolean isEmpty()
    {
        return (major == 0) && (minor == 0) && (revision == 0) && (build == 0);
    }

    @Override
    public boolean equals(Object obj)
    {
        if (obj instanceof Version)
            return compareTo((Version) obj) == 0;

        return super.equals(obj);
    }

    @Override
    public int hashCode()
    {
        // assume 7 bits for each number (0-127 range)
        final int result = (build << 22) | (major << 15) | (minor << 8) | (revision << 1);

        if (beta)
            return result + 1;

        return result;

    }

    @Override
    public String toString()
    {
        if ((major == 0) && (minor == 0) && (revision == 0) && (build == 0) && !beta)
            return "";

        String result;

        result = Integer.toString(major) + ".";
        result += Integer.toString(minor) + ".";
        result += Integer.toString(revision) + ".";
        result += Integer.toString(build);
        if (beta)
            result += " beta";

        return result;
    }

    @Override
    public int compareTo(Version o)
    {
        if (o == null)
            return 1;
        else if (o.isEmpty() || isEmpty())
            return 0;
        else if (o.major < major)
            return 1;
        else if (o.major > major)
            return -1;
        else if (o.minor < minor)
            return 1;
        else if (o.minor > minor)
            return -1;
        else if (o.revision < revision)
            return 1;
        else if (o.revision > revision)
            return -1;
        else if (o.build < build)
            return 1;
        else if (o.build > build)
            return -1;
        else if (o.beta && !beta)
            return 1;
        else if (!o.beta && beta)
            return -1;
        else
            return 0;
    }

    public boolean isGreater(Version version)
    {
        return compareTo(version) > 0;
    }

    public boolean isGreaterOrEqual(Version version)
    {
        return compareTo(version) >= 0;
    }

    public boolean isLower(Version version)
    {
        return compareTo(version) < 0;
    }

    public boolean isLowerOrEqual(Version version)
    {
        return compareTo(version) <= 0;
    }

    public boolean isNewer(Version version)
    {
        return isGreater(version);
    }

    public boolean isNewerOrEqual(Version version)
    {
        return isGreaterOrEqual(version);
    }

    public boolean isOlder(Version version)
    {
        return isLower(version);
    }

    public boolean isOlderOrEqual(Version version)
    {
        return isLowerOrEqual(version);
    }

}
