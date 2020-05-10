import React, { Component } from "react";
import axios from "axios";
import history from "../history";
import Dialog from "./Dialog";

export default class AllRestaurants extends Component {
  constructor(props) {
    super(props);
    this.state = {
      posts: [],
    };
  }

  async componentDidMount() {
    await axios.get("/restaurants").then((res) => {
      const posts = res.data;
      this.setState({ posts });
    });
  }

  restaurantClicked(e) {
    history.replace("/restaurant/" + e);
    console.log(e);
  }

  render() {
    return (
      <div>
        <div className="align-circles">
          <h1 className="bigger-text">Restaurant Catalog</h1>
        </div>
        <div className="item-gap align-circles">
          {this.state.posts.map((postDetail, index) => {
            return (
              <div key={postDetail.id}>
                <button
                  key={postDetail}
                  className="wrap-gap"
                  onClick={() => this.restaurantClicked(postDetail.id)}
                >
                  <img className="small-pic" src={postDetail.imageURL}></img>
                  <div>
                    <div className="restaurant-label">
                      {" "}
                      {postDetail.restaurantName}
                    </div>
                    <div className="description-label">
                      Ratings: {postDetail.ratings}
                    </div>
                    <div className="description-label">
                      {postDetail.category}
                    </div>
                  </div>
                </button>
              </div>
            );
          })}
        </div>
      </div>
    );
  }
}