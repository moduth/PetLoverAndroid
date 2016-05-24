package com.morecruit.domain.model.user;

import com.google.gson.annotations.SerializedName;
import com.morecruit.domain.model.MrResponse;

import java.util.List;

/**
 * @author markzhai on 16/3/4
 * @version 1.0.0
 */
public class ProfileConfig extends MrResponse{

    /**
     * min : 15
     * max : 50
     */
    @SerializedName("age_range")
    private AgeRangeEntity ageRange;

    /**
     * id : 1
     * name : 信息技术
     * sub_items : [{"id":101,"name":"互联网"},{"id":102,"name":"IT"}]
     */
    @SerializedName("industry")
    private List<Industry1> industry;

    /**
     * id : 11
     * name : 北京
     * sub_items : [{"id":110101,"name":"东城区"},{"id":110229,"name":"延庆县"}]
     */
    @SerializedName("city")
    private List<Province> city;

    public void setAgeRange(AgeRangeEntity ageRange) {
        this.ageRange = ageRange;
    }

    public void setIndustry(List<Industry1> industry) {
        this.industry = industry;
    }

    public void setCity(List<Province> city) {
        this.city = city;
    }

    public AgeRangeEntity getAgeRange() {
        return ageRange;
    }

    public List<Industry1> getIndustry() {
        return industry;
    }

    public List<Province> getCity() {
        return city;
    }

    public static class AgeRangeEntity {
        @SerializedName("min")
        private int min;
        @SerializedName("max")
        private int max;

        public void setMin(int min) {
            this.min = min;
        }

        public void setMax(int max) {
            this.max = max;
        }

        public int getMin() {
            return min;
        }

        public int getMax() {
            return max;
        }
    }

    public static class Industry1 {

        @SerializedName("id")
        private int id;

        @SerializedName("name")
        private String name;
        /**
         * id : 101
         * name : 互联网
         */

        @SerializedName("sub_items")
        private List<Industry2> subItems;

        public void setId(int id) {
            this.id = id;
        }

        public void setName(String name) {
            this.name = name;
        }

        public void setSubIndustries(List<Industry2> subItems) {
            this.subItems = subItems;
        }

        public int getId() {
            return id;
        }

        public String getName() {
            return name;
        }

        public List<Industry2> getSubIndustries() {
            return subItems;
        }

        public static class Industry2 {
            @SerializedName("id")
            private int id;
            @SerializedName("name")
            private String name;

            public void setId(int id) {
                this.id = id;
            }

            public void setName(String name) {
                this.name = name;
            }

            public int getId() {
                return id;
            }

            public String getName() {
                return name;
            }
        }
    }

    public static class Province {
        @SerializedName("id")
        private int id;
        @SerializedName("name")
        private String name;
        /**
         * id : 110101
         * name : 东城区
         */

        @SerializedName("sub_items")
        private List<City> subItems;

        public void setId(int id) {
            this.id = id;
        }

        public void setName(String name) {
            this.name = name;
        }

        public void setSubCities(List<City> subItems) {
            this.subItems = subItems;
        }

        public int getId() {
            return id;
        }

        public String getName() {
            return name;
        }

        public List<City> getCities() {
            return subItems;
        }

        public static class City {
            @SerializedName("id")
            private int id;
            @SerializedName("name")
            private String name;

            public void setId(int id) {
                this.id = id;
            }

            public void setName(String name) {
                this.name = name;
            }

            public int getId() {
                return id;
            }

            public String getName() {
                return name;
            }
        }
    }
}
