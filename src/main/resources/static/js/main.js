const openNav = () => {
  const burger = document.querySelector(".burger");
  const nav = document.querySelector("nav");
  const openedNav = document.querySelector(".open-navbar");

  burger.addEventListener("click", () => {
    nav.classList.toggle("opened-navbar");
  });
};

openNav();

const smoothScroll = () => {
  const links = document.querySelectorAll(".nav-to-footer , .scroll-btn-up , .scroll-btn-down");

  for (const link of links) {
    link.addEventListener("click", clickHandler);
  }

  function clickHandler(e) {
    e.preventDefault();
    const href = this.getAttribute("href");
    const offsetTop = document.querySelector(href).offsetTop;

    scroll({
      top: offsetTop,
      behavior: "smooth",
    });
  }
};
smoothScroll();
