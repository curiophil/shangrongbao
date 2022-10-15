<template>
  <div class="personal-main">
    <div class="personal-money">
      <h3><i>借款记录</i></h3>
      <div class="item-detail-head clearfix" data-target="sideMenu" v-for="lend in lends">

        <p>{{ lend }}</p>
        <!-- 标的信息开始 -->
      </div>
    </div>
  </div>
</template>

<script>

export default {
  name: "invest",
  data() {
    return {
      lends: []
    }
  },
  created() {
    this.fetchLendList()
  },
  methods: {
    fetchLendList() {
      this.$axios.$get("/api/core/userInfo/auth/getIndexUserInfo")
          .then((response) => {
            let userInfo = response.data.userIndexVO
            this.$axios.$get("/api/core/lend/list/" + userInfo.userId)
                .then(response => {
                  this.lends = response.data.lendList
                  console.log(this.lends)
                })
          })
    }
  }
}
</script>
