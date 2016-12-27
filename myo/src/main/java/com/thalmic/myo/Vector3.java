package com.thalmic.myo;

public class Vector3
{
    private double mX;
    private double mY;
    private double mZ;

    public Vector3()
    {
        this.mX = (this.mY = this.mZ = 0.0D);
    }

    public Vector3(Vector3 other)
    {
        set(other);
    }

    public Vector3(double x, double y, double z)
    {
        this.mX = x;
        this.mY = y;
        this.mZ = z;
    }

    public void set(Vector3 src)
    {
        this.mX = src.mX;
        this.mY = src.mY;
        this.mZ = src.mZ;
    }

    public double x()
    {
        return this.mX;
    }

    public double y()
    {
        return this.mY;
    }

    public double z()
    {
        return this.mZ;
    }

    public double length()
    {
        return Math.sqrt(this.mX * this.mX + this.mY * this.mY + this.mZ * this.mZ);
    }

    public void add(Vector3 other)
    {
        this.mX += other.mX;
        this.mY += other.mY;
        this.mZ += other.mZ;
    }

    public void subtract(Vector3 other)
    {
        this.mX -= other.mX;
        this.mY -= other.mY;
        this.mZ -= other.mZ;
    }

    public void multiply(double n)
    {
        this.mX *= n;
        this.mY *= n;
        this.mZ *= n;
    }

    public void divide(double n)
    {
        this.mX /= n;
        this.mY /= n;
        this.mZ /= n;
    }

    public void normalize()
    {
        double scale = 1.0D / length();
        multiply(scale);
    }

    public double dot(Vector3 other)
    {
        return this.mX * other.mX + this.mY * other.mY + this.mZ * other.mZ;
    }

    public boolean equals(Object o)
    {
        if (this == o) {
            return true;
        }
        if ((o == null) || (getClass() != o.getClass())) {
            return false;
        }
        Vector3 vector = (Vector3)o;
        if (Double.compare(vector.mX, this.mX) != 0) {
            return false;
        }
        if (Double.compare(vector.mY, this.mY) != 0) {
            return false;
        }
        if (Double.compare(vector.mZ, this.mZ) != 0) {
            return false;
        }
        return true;
    }

    public int hashCode()
    {
        long temp = Double.doubleToLongBits(this.mX);
        int result = (int)(temp ^ temp >>> 32);
        temp = Double.doubleToLongBits(this.mY);
        result = 31 * result + (int)(temp ^ temp >>> 32);
        temp = Double.doubleToLongBits(this.mZ);
        result = 31 * result + (int)(temp ^ temp >>> 32);
        return result;
    }

    public String toString()
    {
        return "Vector3{mX=" + this.mX + ", mY=" + this.mY + ", mZ=" + this.mZ + '}';
    }
}
