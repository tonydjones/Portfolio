function flash()
{
    let words = document.getElementById("world");
    if (words.style.color === "green")
    {
        words.style.color = "blue";
    }
    else
    {
        words.style.color = "green";
    }
}
window.setInterval(flash, 500);