extern crate serialize;
use serialize::json;
use std::num::FloatMath;
use std::io::File;

static PI: f64 = std::f64::consts::PI;
static TAU: f64 = std::f64::consts::PI_2;

fn putpoint(v: &mut Vec<f64>, (x, y, z): (f64, f64, f64)) { v.push(x); v.push(y); v.push(z); }
fn puttri(v: &mut Vec<f64>, p1: (f64, f64, f64), p2: (f64, f64, f64), p3: (f64, f64, f64)) {
    putpoint(v, p1); putpoint(v, p2); putpoint(v, p3);
}
// direct transcription from https://github.com/aweinstock314/correspondence_problem_demo/blob/master/correspondence_problem_demo_main.cpp
fn make_sphereoid(xz_sides: uint, phi_sides: uint, radius: f64) -> Vec<f64> {
    let mut rv = Vec::new();
    let (xzs, ps) = (xz_sides as f64, phi_sides as f64);
    for i1 in range(0, xz_sides*4) {
        for i2 in range(0, phi_sides) {
            let (f1, f2) = (i1 as f64, i2 as f64);
            let (theta1, theta2) = ((TAU * f1) / xzs, TAU * (f1+1.) / xzs);
            let (phi1, phi2) = ((PI*f2)/ps, (PI*(f2+1.))/ps);
            let (height1, height2) = (radius*phi1.cos(), radius*phi2.cos());
            let p1 = (radius*phi1.sin()*theta1.cos(), height1, radius*phi1.sin()*theta1.sin());
            let p2 = (radius*phi2.sin()*theta1.cos(), height2, radius*phi2.sin()*theta1.sin());
            let p3 = (radius*phi1.sin()*theta2.cos(), height1, radius*phi1.sin()*theta2.sin());
            let p4 = (radius*phi2.sin()*theta2.cos(), height2, radius*phi2.sin()*theta2.sin());
            puttri(&mut rv, p1, p2, p3);
            puttri(&mut rv, p2, p3, p4);
        }
    }
    rv
}

fn make_cylinder(xz_sides: uint, radius: f64, height: f64) -> Vec<f64> {
    let mut rv = Vec::new();
    let xzs = xz_sides as f64;
    for ixz in range(0, xz_sides) {
        let fxz = ixz as f64;
        let (theta1, theta2) = ((TAU * fxz) / xzs, (TAU * (fxz+1.)) / xzs);
        let (x1, z1) = (theta1.cos(), theta1.sin());
        let (x2, z2) = (theta2.cos(), theta2.sin());
        let p1 = (x1, 0.0, z1);
        let p2 = (x1, height, z1);
        let p3 = (x2, height, z2);
        let p4 = (x2, 0.0, z2);
        puttri(&mut rv, p1, p2, p3);
        puttri(&mut rv, p3, p4, p1);
        let c0 = (0.0, 0.0, 0.0);
        let ch = (0.0, height, 0.0);
        puttri(&mut rv, p1, c0, p4);
        puttri(&mut rv, p2, ch, p3);
    }
    rv
}

fn main() {
    let unit_sphere = make_sphereoid(50, 50, 1.0);
    let unit_cylinder = make_cylinder(25, 1.0, 1.0);
    File::create(&Path::new("unit_sphere.json")).write_line(json::encode(&unit_sphere).as_slice());
    File::create(&Path::new("unit_cylinder.json")).write_line(json::encode(&unit_cylinder).as_slice());
    //println!("{}", json::encode(&unit_sphere));
}
