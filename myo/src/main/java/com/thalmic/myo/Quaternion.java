package com.thalmic.myo;

public class Quaternion
{
    private double mX;
    private double mY;
    private double mZ;
    private double mW;

    public Quaternion()
    {
        set(0.0D, 0.0D, 0.0D, 1.0D);
    }

    public Quaternion(Quaternion other)
    {
        set(other);
    }

    public Quaternion(double x, double y, double z, double w)
    {
        set(x, y, z, w);
    }

    public void set(Quaternion other)
    {
        this.mX = other.mX;
        this.mY = other.mY;
        this.mZ = other.mZ;
        this.mW = other.mW;
    }

    private void set(double x, double y, double z, double w)
    {
        this.mX = x;
        this.mY = y;
        this.mZ = z;
        this.mW = w;
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

    public double w()
    {
        return this.mW;
    }

    public double length()
    {
        return Math.sqrt(this.mW * this.mW + this.mX * this.mX + this.mY * this.mY + this.mZ * this.mZ);
    }

    public void multiply(Quaternion other)
    {
        double x = this.mW * other.mX + this.mX * other.mW - this.mY * other.mZ + this.mZ * other.mY;
        double y = this.mW * other.mY + this.mX * other.mZ + this.mY * other.mW - this.mZ * other.mX;
        double z = this.mW * other.mZ - this.mX * other.mY + this.mY * other.mX + this.mZ * other.mW;
        double w = this.mW * other.mW - this.mX * other.mX - this.mY * other.mY - this.mZ * other.mZ;
        set(x, y, z, w);
    }

    public void inverse()
    {
        double d = this.mW * this.mW + this.mX * this.mX + this.mY * this.mY + this.mZ * this.mZ;
        set(-this.mX / d, -this.mY / d, -this.mZ / d, this.mW / d);
    }

    public Quaternion normalized()
    {
        double scale = 1.0D / length();
        return new Quaternion(this.mX * scale, this.mY * scale, this.mZ * scale, this.mW * scale);
    }

    public boolean equals(Object o)
    {
        if (this == o) {
            return true;
        }
        if ((o == null) || (getClass() != o.getClass())) {
            return false;
        }
        Quaternion that = (Quaternion)o;
        if (Double.compare(that.mX, this.mX) != 0) {
            return false;
        }
        if (Double.compare(that.mY, this.mY) != 0) {
            return false;
        }
        if (Double.compare(that.mZ, this.mZ) != 0) {
            return false;
        }
        if (Double.compare(that.mW, this.mW) != 0) {
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
        temp = Double.doubleToLongBits(this.mW);
        result = 31 * result + (int)(temp ^ temp >>> 32);
        return result;
    }

    public String toString()
    {
        return "Quaternion{mX=" + this.mX + ", mY=" + this.mY + ", mZ=" + this.mZ + ", mW=" + this.mW + '}';
    }

    public static double roll(Quaternion quat)
    {
        return Math.atan2(2.0D * (quat.mW * quat.mX + quat.mY * quat.mZ), 1.0D - 2.0D * (quat.mX * quat.mX + quat.mY * quat.mY));
    }

    public static double pitch(Quaternion quat)
    {
        return Math.asin(2.0D * (quat.mW * quat.mY - quat.mZ * quat.mX));
    }

    public static double yaw(Quaternion quat)
    {
        return Math.atan2(2.0D * (quat.mW * quat.mZ + quat.mX * quat.mY), 1.0D - 2.0D * (quat.mY * quat.mY + quat.mZ * quat.mZ));
    }
}
