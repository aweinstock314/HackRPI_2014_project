extern crate serialize;
use serialize::json;
use std::num::FloatMath;
use std::io::File;

static PI: f64 = std::f64::consts::PI;
static TAU: f64 = std::f64::consts::PI_2;

// direct transcription from https://github.com/aweinstock314/correspondence_problem_demo/blob/master/correspondence_problem_demo_main.cpp
fn make_sphereoid(xz_sides: uint, phi_sides: uint, radius: f64) -> Vec<f64> {
    let mut rv = Vec::new();
    let put = |v: &mut Vec<f64>, x, y, z| { v.push(x); v.push(y); v.push(z); };
    let (xzs, ps) = (xz_sides as f64, phi_sides as f64);
    for i1 in range(0, xz_sides) {
        for i2 in range(0, phi_sides) {
            let (f1, f2) = (i1 as f64, i2 as f64);
            let (theta1, theta2) = ((TAU * f1) / xzs, TAU * (f1+1.) / xzs);
            let (yseg1, yseg2) = ((PI*f2)/ps, (PI*(f2+1.))/ps);
            let (height1, height2) = (radius*yseg1.cos(), radius*yseg2.cos());
            put(&mut rv, radius*yseg1.sin()*theta1.cos(), height1, radius*yseg1.sin()*theta1.sin());
            put(&mut rv, radius*yseg2.sin()*theta1.cos(), height2, radius*yseg2.sin()*theta1.sin());
            put(&mut rv, radius*yseg1.sin()*theta2.cos(), height1, radius*yseg1.sin()*theta2.sin());
            put(&mut rv, radius*yseg2.sin()*theta2.cos(), height2, radius*yseg2.sin()*theta2.sin());
        }
    }
    rv
}

fn main() {
    let unit_sphere = make_sphereoid(50, 50, 1.0);
    File::create(&Path::new("unit_sphere.json")).write_line(json::encode(&unit_sphere).as_slice());
    //println!("{}", json::encode(&unit_sphere));
}
